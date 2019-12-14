package at.jku.mobilecomputing.airlife.Utilities;
public abstract class onWriteCode<E> {

    protected onWriteCode() {
    }

    public abstract E onExecuteCode() throws Exception;

    public abstract E onSuccess(E result) throws Exception;
}

