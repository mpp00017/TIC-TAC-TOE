/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Manuel
 */
public class PlayerAgent extends Agent {
    
    @Override
    protected void setup(){
        
        System.out.println("Player Agent: "+this.getLocalName()+" is running.");
        //Register the p service in the yellow pages
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
        
        this.addBehaviour(new WaitingOffer());
        this.doDelete();
        
    }
    
    private class WaitingOffer extends CyclicBehaviour{
    @Override
    public void action(){
        ACLMessage msg = myAgent.receive();
        if(msg != null){
            if(msg.getContent().equals(("Offer"))){
                System.out.println("eureca!");
            }
        }else{
            block();
        }
    }
}
    
}


