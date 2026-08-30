import { useState, useEffect } from 'react'
import { getMe, markTutorialSeen } from '@/api/users'

export function useTutorial() {
  const [tutorialOpen, setTutorialOpen] = useState(false)

  useEffect(() => {
    getMe()
      .then(me => { if (!me.hasSeenTutorial) setTutorialOpen(true) })
      .catch(() => {
        if (!localStorage.getItem('gwent_tutorial_seen')) setTutorialOpen(true)
      })
  }, [])

  function openTutorial() { setTutorialOpen(true) }

  function closeTutorial() {
    setTutorialOpen(false)
    markTutorialSeen()
    localStorage.setItem('gwent_tutorial_seen', '1')
  }

  return { tutorialOpen, openTutorial, closeTutorial }
}
