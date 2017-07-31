package part4project.uoa.gather;

import java.util.Date;

/**
 * Created by Ida on 31/07/2017.
 */

class Data{

    private Date createdAt;
    private String type;
    private String value;

    Data(Date createdAt, String type, String value){
        this.createdAt = createdAt;
        this.type = type;
        this.value = value;
    }

    Date getCreatedAt(){
        return createdAt;
    }
    String getType(){
        return type;
    }
    String getValue(){
        return value;
    }
}