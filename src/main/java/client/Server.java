package client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Server 
{
    private StringProperty hostname = new SimpleStringProperty(this, "hostname");
    private StringProperty port = new SimpleStringProperty(this, "port");
    private StringProperty login = new SimpleStringProperty(this, "login");
    
    public Server(String h, String p, String l)
    {
        setHostname(h);
        setPort(p);
        setLogin(l);
    }
    public void setHostname(String value) 
    { 
        hostnameProperty().set(value); 
    }
    public void setPort(String value)
    {
        portProperty().set(value);
    }
    public void setLogin(String value)
    {
        loginProperty().set(value);
    }
    public StringProperty hostnameProperty() 
    {
        return hostname;
    }
    public StringProperty portProperty()
    {
        return port;
    }
    public StringProperty loginProperty()
    {
        return login;
    }
}