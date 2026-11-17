package in.sih25022.railway.model;

import java.time.Instant;

public class Recommendation {
    public String id;
    public String preferredTrainId;
    public String waitingTrainId;
    public String reason;
    public int estimatedDelaySavedMinutes;
    public String status;
    public Instant createdAt = Instant.now();

    public Recommendation(String id, String preferredTrainId, String waitingTrainId,
                          String reason, int estimatedDelaySavedMinutes) {
        this.id=id; this.preferredTrainId=preferredTrainId; this.waitingTrainId=waitingTrainId;
        this.reason=reason; this.estimatedDelaySavedMinutes=estimatedDelaySavedMinutes;
        this.status="PENDING";
    }
}
