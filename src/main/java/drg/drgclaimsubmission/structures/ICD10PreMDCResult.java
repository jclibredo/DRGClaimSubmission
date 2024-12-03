/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

/**
 *
 * @author DRG_SHADOWBILLING
 */
public class ICD10PreMDCResult {
    public ICD10PreMDCResult(){
    }
    
        private String Code;
        private String MDC;
        private String PDC;
        private String CC;
        private String MainCC;
        private String CCRow;
        private String HIV_AX;
        private String Sex;
        private String AccPDX;
        private String AgeDUse;
        private String AgeMin;
        private String AgeMax;
        private String AgeDMin;
        private String Trauma;

    public String getTrauma() {
        return Trauma;
    }

    public void setTrauma(String Trauma) {
        this.Trauma = Trauma;
    }
        

    public String getCode() {
        return Code;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }

    public String getMDC() {
        return MDC;
    }

    public void setMDC(String MDC) {
        this.MDC = MDC;
    }

    public String getPDC() {
        return PDC;
    }

    public void setPDC(String PDC) {
        this.PDC = PDC;
    }

    public String getCC() {
        return CC;
    }

    public void setCC(String CC) {
        this.CC = CC;
    }

    public String getMainCC() {
        return MainCC;
    }

    public void setMainCC(String MainCC) {
        this.MainCC = MainCC;
    }

    public String getCCRow() {
        return CCRow;
    }

    public void setCCRow(String CCRow) {
        this.CCRow = CCRow;
    }

    public String getHIV_AX() {
        return HIV_AX;
    }

    public void setHIV_AX(String HIV_AX) {
        this.HIV_AX = HIV_AX;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String Sex) {
        this.Sex = Sex;
    }

    public String getAccPDX() {
        return AccPDX;
    }

    public void setAccPDX(String AccPDX) {
        this.AccPDX = AccPDX;
    }

    public String getAgeDUse() {
        return AgeDUse;
    }

    public void setAgeDUse(String AgeDUse) {
        this.AgeDUse = AgeDUse;
    }

    public String getAgeMin() {
        return AgeMin;
    }

    public void setAgeMin(String AgeMin) {
        this.AgeMin = AgeMin;
    }

    public String getAgeMax() {
        return AgeMax;
    }

    public void setAgeMax(String AgeMax) {
        this.AgeMax = AgeMax;
    }

    public String getAgeDMin() {
        return AgeDMin;
    }

    public void setAgeDMin(String AgeDMin) {
        this.AgeDMin = AgeDMin;
    }
         
    
    
    
}