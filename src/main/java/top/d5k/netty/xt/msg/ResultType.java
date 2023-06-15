package top.d5k.netty.xt.msg;


import lombok.Getter;

@Getter
public enum ResultType {
    SUCCESS((byte) 0),
    FAIL((byte) -1);

    private final byte value;

    ResultType(byte value) {
        this.value = value;
    }

}
