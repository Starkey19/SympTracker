package jpstarkey.symptracker;

/**
 * Created by Joshs on 30/03/2017.
 */

//Symptom DB model

public class Symptom
{
    int _id;
    String name;
    String description;
    int pain;

    //Default empty constructor
    public Symptom()
    {

    }

    public Symptom(String name, String description, int pain)
    {
        this.name = name;
        this.description = description;
        this.pain = pain;
    }

    public void set_id(int _id)
    {
        this._id = _id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setPain(int pain)
    {
        this.pain = pain;
    }

    public int get_id()
    {
        return _id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getPain()
    {
        return pain;
    }
}
