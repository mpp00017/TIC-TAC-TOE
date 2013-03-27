/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.Agent;
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
            }
            return answer;
        }
        
    }
       
}


