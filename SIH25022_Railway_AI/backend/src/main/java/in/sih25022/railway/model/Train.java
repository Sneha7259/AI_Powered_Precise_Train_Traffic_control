package in.sih25022.railway.model;

public class Train {
    public String id;
    public String name;
    public String type;
    public int priority;          // 1 = highest operational priority in this simulation
    public double speedKmph;
    public double lengthMeters;
    public double positionKm;
    public double destinationKm;
    public int delayMinutes;
    public String status;

    public Train(String id, String name, String type, int priority, double speedKmph,
                 double lengthMeters, double positionKm, double destinationKm,
                 int delayMinutes, String status) {
        this.id=id; this.name=name; this.type=type; this.priority=priority;
        this.speedKmph=speedKmph; this.lengthMeters=lengthMeters;
        this.positionKm=positionKm; this.destinationKm=destinationKm;
        this.delayMinutes=delayMinutes; this.status=status;
    }
}
