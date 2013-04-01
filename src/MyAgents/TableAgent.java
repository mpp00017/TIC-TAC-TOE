/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 *
 * @author Manuel
 */
public class TableAgent extends Agent {
    AID playerAgents[];
    AID jugadores[] = new AID[2];
    TicTacToeFrame myGUI;
    protected AID[] getJugadores(){return jugadores;}
    
    @Override
    protected void setup(){
        
        myGUI = new TicTacToeFrame(this);
        myGUI.setVisible(true);
        
        System.out.println("Table Agent: "+this.getLocalName()+" is running.");
        
        
        //Search Player Agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            playerAgents = new AID[result.length];
            for(int i=0; i < result.length;i++){
                playerAgents[i] = result[i].getName();
            }
        }catch(FIPAException fe){}
        
        
         ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
         msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
         msg.setLanguage("English");
         msg.setContent("Would you like to play?");
         
        for(int i=0;i<playerAgents.length;i++){
            msg.addReceiver(playerAgents[i]);
        }
        
        this.addBehaviour(new CreateGame(this,msg));
    }
    
    private class StartGame extends Behaviour{
        boolean finish = false;
        String lastMov = "";
        int player = 1;
        StartGame(AID[] jugadores){};
        
        @Override
        public void action() {
            myGUI.setTextPlayers(jugadores[0].getLocalName(), jugadores[1].getLocalName());
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setLanguage("English");
            msg.setContent(lastMov);
            msg.addReceiver(jugadores[player]);
            send(msg);
            
            msg = myAgent.receive();
            if(msg != null){
                if(msg.getContent().length() == 1){
                    lastMov = msg.getContent();
                    if(player==1){
                        player=0;
                    }else{
                        player=1;
                    }
                }else if(msg.getContent().length() == 3){
                    //ha ganado
                    finish = true;
                }
            }else{
                block();
            }
        }

        @Override
        public boolean done() {
            return finish;
        }
        
    }
    
    private class CreateGame extends ProposeInitiator{
        
        private int vacantes = 2;
        
        CreateGame(Agent a, ACLMessage message){
            super(a,message);
        }
                
        @Override
        protected void handleAcceptProposal(ACLMessage accepted) {
            if(vacantes == 2){
                jugadores[0] = accepted.getSender();
                vacantes--;
            }else if(vacantes == 1){
                jugadores[1] = accepted.getSender();
                vacantes--;
                myAgent.addBehaviour(new StartGame(jugadores));
            }
        }
        
    }
}
