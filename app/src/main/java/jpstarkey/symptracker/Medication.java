package jpstarkey.symptracker;

import static android.R.attr.id;

/**
 * Created by Joshs on 30/03/2017.
 */

public class Medication
{
    int _id;
    String name;
    String description;
    int amount;
    int frequency;

    //Default empty constructor
    public Medication()
    {

    }

    public Medication(String name, String description, int amount, int frequency)
    {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.frequency = frequency;
    }

    public int getId()
    {
        return _id;
    }

    public void setId(int id)
    {
        this._id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public void setFrequency(int frequency)
    {
        this.frequency = frequency;
    }
}
