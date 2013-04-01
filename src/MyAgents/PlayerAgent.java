/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;

/**
 *
 * @author Manuel
 */
public class PlayerAgent extends Agent {
    
    boolean playing = false;
    
    @Override
    protected void setup(){
        
        System.out.println("Player Agent: "+this.getLocalName()+" is running.");
        //Register the player service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName("JADE-Player");
        dfd.addServices(sd);
        try{
            DFService.register(this,dfd);
        }
        catch(FIPAException fe){}
        
        //this.addBehaviour(new WaitingOffer());
        
        //Creamos la plantilla a emplear, para solo recibir mensajes con el protocolo FIPA_PROPOSE y la performativa PROPOSE
        MessageTemplate template = ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
 
        //Añadimos el comportamiento "responderSalirClase()"
        this.addBehaviour(new ResponderOffer(this, template));
        
    }
    
    private class ResponderOffer extends ProposeResponder{

        private ResponderOffer(Agent a, MessageTemplate template) {
            super(a,template);
        }
        
        @Override
        protected ACLMessage prepareResponse(ACLMessage proposal){
            ACLMessage answer = proposal.createReply();
            if(playing){
                answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }else{
                answer.setPerformative((ACLMessage.ACCEPT_PROPOSAL));
                playing = true;
                myAgent.addBehaviour(new sendMovement());
            }
            return answer;
        }
        
    }
       
    private class sendMovement extends Behaviour{
        boolean finish = false;
        int table[][] = new int[3][3];
        String movement;
        int mov;

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if(msg != null){
                if(!"".equals(msg.getContent())){
                    movement = msg.getContent();
                    switch(movement){
                        case "1":
                            table[0][0]=2;
                            break;
                        case "2":
                            table[0][1]=2;
                            break;
                        case "3":
                            table[0][2]=2;
                            break;
                        case "4":
                            table[1][0]=2;
                            break;
                        case "5":
                            table[1][1]=2;
                            break;
                        case "6":
                            table[1][2]=2;
                            break;
                        case "7":
                            table[2][0]=2;
                            break;
                        case "8":
                            table[2][1]=2;
                            break;
                        case "9":
                            table[2][2]=2;
                            break;
                    }
                }
            
                mov=0;
                boolean encontrado = false;
                for(int i=0;i<3;i++)
                    for(int j=0;j<3;j++)
                        if(!encontrado){
                            mov++;
                            if(table[i][j] == 0){
                                encontrado = true;
                                table[i][j] = 1;
                            }
                        }
                //finish = partidaTerminada();.....
                ACLMessage msgToTable = new ACLMessage(ACLMessage.INFORM);
                msgToTable.setLanguage("English");
                msgToTable.setContent(String.valueOf(mov));
                msgToTable.addReceiver(msg.getSender());
                send(msgToTable);
                //System.out.println(myAgent.getLocalName()+" : "+mov);

            }else{
                block();
            }
            
        }

        @Override
        public boolean done() {
            return finish;
        }
        
    }
}


