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
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ProposeResponder;

/**
 * Agente jugador de Tic-Tac-Toe
 *
 * @author Manuel Pancorbo Pestaña, Juan Cazalla Estrella
 */
public class PlayerAgent extends Agent {
    
    boolean playing = false;
    boolean finish = false;
    int table[][] = new int[3][3];
    String movement;
    int mov;
    
    /**
     * Describe aqui picha
     */
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
        }catch(FIPAException fe){}
        
        //Creamos la plantilla a emplear, para solo recibir mensajes con el protocolo FIPA_PROPOSE y la performativa PROPOSE
        MessageTemplate template = ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        
        MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate plantilla = MessageTemplate.and(protocolo, performativa);
 
        this.addBehaviour(new ResponderOffer(this, template));
    }
    
    /**
     * Describe aqui picha
     */
    private class ResponderOffer extends ProposeResponder{

        /**
         * Describe aqui picha 
         * 
         * @param a Agente... lo que tu veas y así todos los atributos
         * @param template 
         */
        private ResponderOffer(Agent a, MessageTemplate template) {
            super(a,template);
        }
        
         /**
          * Describe aqui picha
          * 
          * @param proposal
          * @return 
          */
        @Override
        protected ACLMessage prepareResponse(ACLMessage proposal){
            ACLMessage answer = proposal.createReply();
            if(playing)
                answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
            else{
                answer.setPerformative((ACLMessage.ACCEPT_PROPOSAL));
                ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
                msg.setContent("Am I playing?");
                msg.addReceiver(proposal.getSender());
                
                myAgent.addBehaviour(new AmIPlaying(myAgent, msg));
            }
            return answer;
        }
    }
    
    /**
     * Clase que se encarga de comprobar si un agente está jugando
     */
    private class AmIPlaying extends AchieveREInitiator{
        /**
         * Describe aqui picha
         * 
         * @param agent
         * @param msg 
         */
        public AmIPlaying(Agent agent, ACLMessage msg){
            super(agent,msg);
        }
        
       /**
        * Manejador de mensajes inform.
        * En este método, el jugador recibe el mensaje en el que se le indica si
        * va a jugar o no la partida.
        * 
        * @param inform Mensaje recibido
        */
        @Override
        protected void handleInform(ACLMessage inform){
            switch (inform.getContent()) {
                case "YES":
                    System.out.println(myAgent.getLocalName()+": Que voy a jugar!! :D");
                    playing = true;
                    MessageTemplate mt;
                    mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    myAgent.addBehaviour(new SendMovement(myAgent, mt));
                    break;
                case "NO":
                    System.out.println(myAgent.getLocalName()+": Pues resulta de que no voy a jugar.");
                    playing = false;
                    break;
            }
        }
    }
    
    /**
     * Clase que se encarga de enviar movimientos al tablero
     */
    private class SendMovement extends AchieveREResponder{
        
        /**
         * Constructor de la clase SendMovement
         * 
         * @param a Agente que va a enviar el movimiento
         * @param template Mensaje plantilla
         */
        public SendMovement(Agent a, MessageTemplate template ){
            super(a,template);
        }
        
        /**
         * Describe aqui picha
         * 
         * @param request
         * @return Explica lo que devuelve tambien en los return
         */
        @Override
        protected ACLMessage handleRequest(ACLMessage request){
            
            ACLMessage agree = request.createReply();            
            agree.setPerformative(ACLMessage.AGREE);
            
            return agree;
        }
        
        /**
         * Describe aqui picha
         * 
         * @param request
         * @param response
         * @return 
         */
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request,ACLMessage response){
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            
            if(request.getContent().length()>1){
                playing = false;
                for(int i=0; i<3; i++)
                    for(int j=0; j<3; j++)
                        table[i][j]=0;
                if("Empate".equals(request.getContent())) inform.setContent("Draw!");
                else inform.setContent("Congratulations!");
                
            }else if(request.getContent().length()<=1){
                if(!"".equals(request.getContent())){
                    movement = request.getContent();
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
            
                mov = generateMov();
                inform.setContent(String.valueOf(mov));

                if(table[0][0]==1){
                    if(table[0][1]==1)
                        if(table[0][2]==1)
                            inform.setContent(String.valueOf(mov) + "123");

                    if(table[1][0]==1)
                        if(table[2][0]==1)
                            inform.setContent(String.valueOf(mov) + "147");

                    if(table[1][1]==1)
                        if(table[2][2]==1)
                            inform.setContent(String.valueOf(mov) + "159");
                }

                if (table[1][1]==1){
                    if(table[0][1]==1)
                        if(table[2][1]==1)
                            inform.setContent(String.valueOf(mov) + "258");

                    if(table[1][0]==1)
                        if(table[1][2]==1) 
                            inform.setContent(String.valueOf(mov) + "456");

                    if(table[0][2]==1)
                        if(table[2][0]==1)
                            inform.setContent(String.valueOf(mov) + "357");
                }
                
                if (table[2][2]==1){
                    if(table[2][1]==1)
                        if(table[2][0]==1)
                            inform.setContent(String.valueOf(mov) + "789");

                    if(table[1][2]==1)
                        if(table[0][2]==1)
                            inform.setContent(String.valueOf(mov) + "369");
                }

                if (inform.getContent().length()==4) {
                    playing = false;
                    for(int i=0; i<3; i++)
                        for(int j=0; j<3; j++)
                            table[i][j]=0;
                }
            }
            return inform;           
        }
        
        /**
         * Metodo que genera el siguiente movimiento a realizar por el jugador.
         * La generación es de forma aleatoria entre las casillas libres.
         * 
         * @return Casilla a la que se debe realizar el siguiente movimiento.
         */
        private int generateMov() {
            boolean found = false;
            int move;
            do{
                move=(int) (Math.random() * 9)+1;
                switch(move){
                case 1:
                    if(table[0][0]==0){
                        table[0][0]=1;
                        found = true;
                    }
                    break;
                case 2:
                    if(table[0][1]==0){
                        table[0][1]=1;
                        found = true;
                    }
                    break;
                case 3:
                    if(table[0][2]==0){
                        table[0][2]=1;
                        found = true;
                    }
                    break;
                case 4:
                    if(table[1][0]==0){
                        table[1][0]=1;
                        found = true;
                    }
                    break;
                case 5:
                    if(table[1][1]==0){
                        table[1][1]=1;
                        found = true;
                    }
                    break;
                case 6:
                    if(table[1][2]==0){
                        table[1][2]=1;
                        found = true;
                    }
                    break;
                case 7:
                    if(table[2][0]==0){
                        table[2][0]=1;
                        found = true;
                    }
                    break;
                case 8:
                    if(table[2][1]==0){
                        table[2][1]=1;
                        found = true;
                    }
                    break;
                case 9:
                    if(table[2][2]==0){
                        table[2][2]=1;
                        found = true;
                    }
                    break;
                }
            }while(!found);
            
            return move;
        }
    }
}
       
    