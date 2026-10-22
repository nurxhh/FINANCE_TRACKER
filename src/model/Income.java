/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import  java.time.LocalDate;
public class  Income implements Transactable {
    private final int Id;
    private final double Amount;
    private final LocalDate Date;
    private final String Category;
    private final String Description;
    public Income (int Id,double Amount,LocalDate Date, String Category,String Description)
    {
        this.Id=Id;
        this.Amount=Amount;
        this.Date=Date;
        this.Category=Category;
        this.Description=Description;
    }
    @Override
    public int getId(){
        return Id;
    }
    @Override
    public double getAmount(){
        return Amount;
    }

    /**
     *
     * @return
     */
    @Override
    public LocalDate getDate(){
        return Date;
    }
    @Override
    public String getCategory(){
        return Category;
    }
    @Override
    public String getDescription(){
        return Description;
    }
    
}
