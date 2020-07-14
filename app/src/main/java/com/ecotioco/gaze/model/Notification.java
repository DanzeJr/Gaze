package com.ecotioco.gaze.model;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {

    public Long id;
    public String title;
    public String content;
    public String type;
    public Long objectId;
    public String image;
    public String code;
    public String status;

    // extra attribute
    public Boolean read = false;
    public Date createdDate;

}
