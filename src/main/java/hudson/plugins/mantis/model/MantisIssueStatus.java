/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.mantis.model;
import java.io.Serializable;
/**
 *
 * @author FabreLucaS
 */

/*  Enum Issue Status to match Status and add a Filter  */
public enum MantisIssueStatus {
    
    New,
    acknowledged, 
    pending,
    assigned,
    inprogress, 
    resolved, 
    verified, 
    rejected, 
    closed,    
}
