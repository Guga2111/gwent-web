#!/usr/bin/env python3
"""Testa broadcast de erros via WebSocket/STOMP."""

import json
import subprocess
import threading
import time
import websocket

BASE_URL = "http://localhost:8080"
WS_URL = "ws://localhost:8080/ws/websocket"

BLUE = "\033[0;34m"
GREEN = "\033[0;32m"
RED = "\033[0;31m"
YELLOW = "\033[1;33m"
NC = "\033[0m"


def info(msg):
    print(f"{BLUE}[INFO]{NC} {msg}")


def ok(msg):
    print(f"{GREEN}[OK]{NC} {msg}")


def fail(msg):
    print(f"{RED}[FAIL]{NC} {msg}")


def warn(msg):
    print(f"{YELLOW}[WARN]{NC} {msg}")


# --- HTTP helpers ---

def curl_with_headers(path, body=None, token=None):
    """Returns (headers_str, body_str)."""
    cmd = ["curl", "-s", "-D", "-", f"{BASE_URL}{path}", "-X", "POST",
           "-H", "Content-Type: application/json"]
    if body:
        cmd += ["-d", json.dumps(body)]
    if token:
        cmd += ["-H", f"Authorization: Bearer {token}"]
    result = subprocess.run(cmd, capture_output=True, text=True)
    output = result.stdout
    # Split headers from body (double CRLF or double LF)
    for sep in ["\r\n\r\n", "\n\n"]:
        if sep in output:
            parts = output.split(sep, 1)
            return parts[0], parts[1] if len(parts) > 1 else ""
    return output, ""


def curl_json(path, body=None, token=None):
    """POST and return parsed JSON body."""
    cmd = ["curl", "-s", f"{BASE_URL}{path}", "-X", "POST",
           "-H", "Content-Type: application/json"]
    if body:
        cmd += ["-d", json.dumps(body)]
    if token:
        cmd += ["-H", f"Authorization: Bearer {token}"]
    result = subprocess.run(cmd, capture_output=True, text=True)
    return json.loads(result.stdout) if result.stdout.strip() else None


def register(email, username, password):
    curl_json("/api/auth/register", {"email": email, "username": username, "password": password})


def login(email, password):
    headers, _ = curl_with_headers("/authenticate", {"email": email, "password": password})
    for line in headers.split("\n"):
        if line.lower().startswith("authorization:"):
            return line.split("Bearer ", 1)[1].strip()
    return None


def create_game(token):
    data = curl_json("/api/games", token=token)
    return data["gameId"] if data else None


def join_game(game_id, token):
    curl_json(f"/api/games/{game_id}/join", token=token)


# --- STOMP helpers ---

def stomp_frame(command, headers=None, body=""):
    headers = headers or {}
    lines = [command]
    for k, v in headers.items():
        lines.append(f"{k}:{v}")
    lines.append("")
    lines.append(body)
    return "\n".join(lines) + "\0"


def run_stomp_test(test_name, token, subscribe_dest, send_dest, body):
    info(f"Teste: {test_name}")

    received = []
    connected_event = threading.Event()
    done_event = threading.Event()

    def on_message(ws, message):
        received.append(message)
        # Check if we got a CONNECTED frame
        if message.startswith("CONNECTED"):
            connected_event.set()
        # Check if we got a MESSAGE (error response)
        if message.startswith("MESSAGE") or message.startswith("ERROR"):
            done_event.set()

    def on_open(ws):
        def run():
            # CONNECT
            ws.send(stomp_frame("CONNECT", {
                "Authorization": f"Bearer {token}",
                "accept-version": "1.2",
                "heart-beat": "0,0"
            }))

            # Wait for CONNECTED
            if not connected_event.wait(timeout=3):
                fail("Timeout esperando CONNECTED")
                ws.close()
                return

            # SUBSCRIBE
            ws.send(stomp_frame("SUBSCRIBE", {
                "id": "sub-errors",
                "destination": subscribe_dest
            }))

            time.sleep(0.3)

            # SEND command
            ws.send(stomp_frame("SEND", {
                "destination": send_dest,
                "content-type": "application/json"
            }, body))

            # Wait for error response
            done_event.wait(timeout=3)
            time.sleep(0.3)
            ws.close()

        threading.Thread(target=run, daemon=True).start()

    def on_error(ws, error):
        pass

    ws = websocket.WebSocketApp(
        WS_URL,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
    )

    ws_thread = threading.Thread(target=ws.run_forever, daemon=True)
    ws_thread.start()
    ws_thread.join(timeout=8)

    # Parse results
    error_found = False
    for msg in received:
        if "MESSAGE" in msg and "{" in msg:
            json_str = msg[msg.index("{"):msg.rindex("}") + 1]
            try:
                parsed = json.loads(json_str)
                ok(f"Erro recebido: {json.dumps(parsed, indent=2)}")
                error_found = True
            except json.JSONDecodeError:
                pass
        elif msg.startswith("ERROR"):
            warn(f"STOMP ERROR frame: {msg.strip()}")

    if not error_found:
        warn("Nenhum ErrorDto recebido")
        if received:
            warn(f"Frames recebidos: {len(received)}")
            for r in received:
                print(f"  > {r.strip()[:120]}")

    print()


# --- Main ---

def main():
    # Register users
    for email, username in [
        ("player1@test.com", "Player1"),
        ("player2@test.com", "Player2"),
        ("intruder@test.com", "Intruder"),
    ]:
        info(f"Registrando {email}...")
        register(email, username, "123456")

    # Login
    info("Login player1...")
    token1 = login("player1@test.com", "123456")
    if not token1:
        fail("Falha ao obter token player1")
        return
    ok("Token player1 obtido")

    info("Login player2...")
    token2 = login("player2@test.com", "123456")
    ok("Token player2 obtido")

    info("Login intruder...")
    token3 = login("intruder@test.com", "123456")
    ok("Token intruder obtido")

    # Create game
    info("Criando jogo com player1...")
    game_id = create_game(token1)
    if not game_id:
        fail("Falha ao criar jogo")
        return
    ok(f"Jogo criado: {game_id}")

    info("Player2 entrando no jogo...")
    join_game(game_id, token2)
    ok("Player2 entrou")

    print()
    print("=" * 50)
    print(" Jogo pronto. Executando testes de erro...")
    print("=" * 50)
    print()

    fake_game_id = "00000000-0000-0000-0000-000000000000"

    # Test 1: GameNotFoundException
    run_stomp_test(
        "GameNotFoundException (gameId inexistente)",
        token1,
        f"/topic/games/{fake_game_id}/errors",
        f"/app/games/{fake_game_id}/command",
        json.dumps({"playerId": "player1@test.com", "commandType": "PASS", "cardId": None, "targetRow": None}),
    )

    # Test 2: PlayerNotInGameException
    run_stomp_test(
        "PlayerNotInGameException (intruder envia comando)",
        token3,
        f"/topic/games/{game_id}/errors",
        f"/app/games/{game_id}/command",
        json.dumps({"playerId": "intruder@test.com", "commandType": "PASS", "cardId": None, "targetRow": None}),
    )

    # Test 3: CardNotFoundException
    run_stomp_test(
        "CardNotFoundException (carta inexistente na mao)",
        token1,
        f"/topic/games/{game_id}/errors",
        f"/app/games/{game_id}/command",
        json.dumps({"playerId": "player1@test.com", "commandType": "PLAY_CARD", "cardId": "FAKE_CARD", "targetRow": "MELEE"}),
    )

    # Test 4: GwentException (PASS na fase REDRAW)
    run_stomp_test(
        "GwentException (PASS durante fase REDRAW)",
        token1,
        f"/topic/games/{game_id}/errors",
        f"/app/games/{game_id}/command",
        json.dumps({"playerId": "player1@test.com", "commandType": "PASS", "cardId": None, "targetRow": None}),
    )

    print("=" * 50)
    print(" Testes finalizados")
    print("=" * 50)


if __name__ == "__main__":
    main()
