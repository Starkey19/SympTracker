package jpstarkey.symptracker;

/**
 * Created by Joshs on 09/04/2017.
 */

public class DailyLog
{
    int _id;
    int date;
    int pain;

    public DailyLog()
    {

    }

    public DailyLog(int date, int pain)
    {
        this.date = date;
        this.pain = pain;
    }

    public int get_id()
    {
        return _id;
    }

    public void set_id(int _id)
    {
        this._id = _id;
    }

    public int getDate()
    {
        return date;
    }

    public void setDate(int date)
    {
        this.date = date;
    }

    public int getPain()
    {
        return pain;
    }

    public void setPain(int pain)
    {
        this.pain = pain;
    }
}
