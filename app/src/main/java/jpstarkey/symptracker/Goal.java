package jpstarkey.symptracker;

/**
 * Created by Joshs on 09/04/2017.
 */

public class Goal
{
    int _id;
    String name;
    String description;
    int accomplishDate;
    boolean accomplished;

    public Goal()
    {

    }

    public Goal(String name, String description, int accomplishDate, boolean accomplished)
    {
        this.name = name;
        this.description = description;
        this.accomplishDate = accomplishDate;
        this.accomplished = accomplished;
    }

    public int get_id()
    {
        return _id;
    }

    public void set_id(int _id)
    {
        this._id = _id;
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

    public int getAccomplishDate()
    {
        return accomplishDate;
    }

    public void setAccomplishDate(int accomplishDate)
    {
        this.accomplishDate = accomplishDate;
    }

    public boolean isAccomplished()
    {
        return accomplished;
    }

    public void setAccomplished(boolean accomplished)
    {
        this.accomplished = accomplished;
    }
}
