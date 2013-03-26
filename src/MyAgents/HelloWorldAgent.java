/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;


import jade.core.Agent;

/**
   This example shows a minimal agent that just prints "Hallo World!" 
   and then terminates.
   @author Giovanni Caire - TILAB
 */
public class HelloWorldAgent extends Agent {

  @Override
  protected void setup() {

  	System.out.println("Hola Mundo! Mi nombre es "+getLocalName());

  	// Esto peta fijo!

  	// Make this agent terminate
  	doDelete();
        
  } 
}

