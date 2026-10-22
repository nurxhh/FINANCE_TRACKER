/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author USER
 */
public interface Transactable {
    int getId();
    double getAmount();
    LocalDate getDate();
    String  getCategory();
    String getDescription();
    
}
