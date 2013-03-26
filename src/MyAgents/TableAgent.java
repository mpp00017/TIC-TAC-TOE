/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Manuel
 */
public class TableAgent extends Agent {
    AID playerAgents[];
    @Override
    protected void setup(){
        
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
        
        for(int i=0;i<playerAgents.length;i++){
            System.out.println(playerAgents[i]);
        }
        
        for(int i=0;i<playerAgents.length;i++){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(playerAgents[i]);
            msg.setLanguage("English");
            msg.setContent("Offer");
            send(msg);
        }
    }
}
