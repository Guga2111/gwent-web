package com.gwent.engine.command;

public sealed interface GameCommand permits PlayCardCommand, PassCommand, MulliganCommand, UseLeaderCommand, ResolveMedicCommand, ConfirmMulliganCommand, ResolveLeaderCommand, ResolveScoiataelCommand { }
