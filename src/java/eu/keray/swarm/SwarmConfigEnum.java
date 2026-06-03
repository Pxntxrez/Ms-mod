package eu.keray.swarm;

public enum SwarmConfigEnum {
    ALWAYS(true, true),
    NEVER(false, false),
    FULLMOON(false, true);

    public final boolean fullmoon;
    public final boolean normal;

    SwarmConfigEnum(boolean normal, boolean fullmoon) {
        this.fullmoon = fullmoon;
        this.normal = normal;
    }

    public boolean isSwawrm(boolean fmoon) {
        return (this.normal || (this.fullmoon && fmoon));
    }
}
