package spaceInvaders.view

import scalafxml.core.macros.sfxml
import scalafx.application.Platform
import spaceInvaders.Client
import spaceInvaders.GameClient
import spaceInvaders.GameServer
@sfxml
class MenuLayoutController() {
    def handleSoloClick():Unit = {
        Client.initialiseGame()
    }

    def handleLocalClick():Unit = {

        Client.playerClient ! GameClient.StartJoin()
        
        Client.setGameScene()
    }

}