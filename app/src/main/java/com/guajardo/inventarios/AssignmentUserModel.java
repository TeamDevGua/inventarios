package com.guajardo.inventarios;

public class AssignmentUserModel {

    private String Id;
    private String Desc;
    private String Cant;
    private String idCapAud;
    private String userConto;
    private String fecuserConto;
    private String tipocap;

    public AssignmentUserModel(String Id, String Desc, String Cant, String idCapAud, String userConto, String fecuserConto, String tipocap) {
        this.Id = Id;
        this.Desc = Desc;
        this.Cant = Cant;
        this.idCapAud = idCapAud;
        this.userConto = userConto;
        this.fecuserConto = fecuserConto;
        this.tipocap = tipocap;
    }

    public String getId() {
        return Id;
    }

    public String getCant() {
        return Cant;
    }

    public String getUserConto() {
        return userConto;
    }

    public String getFecuserConto() {
        return fecuserConto;
    }

    public String getidCapAud() {
        return idCapAud;
    }

    public String getTipocap() {
        return tipocap;
    }


}
