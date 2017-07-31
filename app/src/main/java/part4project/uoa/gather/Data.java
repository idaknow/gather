package part4project.uoa.gather;

import java.util.Date;

/**
 * Created by Ida on 31/07/2017.
 */

public class Data{

    Date createdAt;
    String type;
    String value;

    Data(Date createdAt, String type, String value){
        this.createdAt = createdAt;
        this.type = type;
        this.value = value;
    }

    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setValue(String value){
        this.value = value;
    }

    public Date getCreatedAt(){
        return createdAt;
    }
    public String getType(){
        return type;
    }
    public String getValue(){
        return value;
    }
}