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
     * Método de inicialización del agente 
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
     * Método que se llama al destruir el agente. 
     */
    protected void takeDown(){
        System.out.println(this.getLocalName() + ": Dejo de jugar, hasta otra!");
    }
    
    /**
     * Clase que implementa el protocolo CFP encargda de responder peticiones de los tableros para iniciar partida, se aceptará siempre mientras no se esté jugando otra partida
     */
    private class ResponderOffer extends ProposeResponder{

        /**
         * Constructor de la clase ResponderOffer 
         * 
         * @param a agente que lanza la tarea
         * @param template plantilla de mensaje que se recibirá
         */
        private ResponderOffer(Agent a, MessageTemplate template) {
            super(a,template);
        }
        
         /**
          * Método que se encarga de crear la respuesta, afirmativa siempre y cuando no se esté jugando ya
          * 
          * @param proposal mensaje recibido con la petición del tablero
          * @return answer respuesta que se le mandará al tablero
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
     * Clase que se encarga de consultar si un agente está jugando mediante el protocolo Query
     */
    private class AmIPlaying extends AchieveREInitiator{
        /**
         * Constructor de la clase AmIPlaying
         * 
         * @param agent agente que lanza la tarea
         * @param msg mensaje que enviará
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
                    System.out.println(myAgent.getLocalName()+": He sido seleccionado para jugar la partida!");
                    playing = true;
                    MessageTemplate mt;
                    mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    myAgent.addBehaviour(new SendMovement(myAgent, mt));
                    break;
                case "NO":
                    System.out.println(myAgent.getLocalName()+": No he sido seleccionado para esta partida");
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
         * Manejador de la respuesta
         * 
         * @param request
         * @return agree Devuelve un mensaje indicando que se acepta la petición
         */
        @Override
        protected ACLMessage handleRequest(ACLMessage request){
            
            ACLMessage agree = request.createReply();            
            agree.setPerformative(ACLMessage.AGREE);
            
            return agree;
        }
        
        /**
         * Método que se encarga de preparar el mensaje de respuesta al tablero con el movimiento a realizar
         * 
         * @param request Mensaje con la petición
         * @param response
         * @return inform El mensaje con la respuesta a enviar al tablero
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
       
    