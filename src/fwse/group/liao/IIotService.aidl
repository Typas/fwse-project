package fwse.group.liao;

interface IIotService{
    void send(in Bundle bundle);
    Bundle receive();
}
