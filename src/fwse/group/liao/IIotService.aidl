package fwse.group.liao;
import android.os.Bundle;

interface IIotService{
    void send(in Bundle bundle);
    Bundle receive();
}
