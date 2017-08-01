package part4project.uoa.gather;

import java.util.Date;

/**
 * Created by Ida on 31/07/2017.
 * This is a DATA object that contains the activity, time it was created and the type of activity
 * This is used to contain all the social or general objects
 */

class Data{

    // initialised variables
    private Date createdAt;
    private DataCollectionType type;
    private String value;

    /**
     * Data Constructor
     * @param createdAt : The time the activity took place
     * @param type : The data type (in string format)
     * @param value : The activity data
     */
    Data(Date createdAt, DataCollectionType type, String value){
        this.createdAt = createdAt;
        this.type = type;
        this.value = value;
    }

    // Getters for each variable
    Date getCreatedAt(){
        return createdAt;
    }
    DataCollectionType getType(){
        return type;
    }
    String getValue(){
        return value;
    }
}