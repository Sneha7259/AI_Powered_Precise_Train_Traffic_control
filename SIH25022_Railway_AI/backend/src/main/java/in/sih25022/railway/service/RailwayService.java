package in.sih25022.railway.service;

import in.sih25022.railway.model.Train;
import in.sih25022.railway.model.Recommendation;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RailwayService {
    // Simplified 24 km single-line section. Loop points are at km 8 and km 16.
    private final double sectionLengthKm = 24.0;
    private final List<Double> loopPointsKm = List.of(8.0, 16.0);
    private final Map<String, Train> trains = new LinkedHashMap<>();
    private final Map<String, Recommendation> recommendations = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);
    private long simulationMinute = 0;

    public RailwayService() {
        seed();
    }

    private void seed() {
        trains.put("TR-101", new Train("TR-101","Intercity Express","PASSENGER",2,72,420,2.0,24,8,"RUNNING"));
        trains.put("TR-205", new Train("TR-205","Superfast Express","PASSENGER",1,82,390,18.0,0,3,"RUNNING"));
        trains.put("TR-310", new Train("TR-310","Freight 310","FREIGHT",4,42,650,5.5,24,18,"RUNNING"));
        trains.put("TR-411", new Train("TR-411","Passenger 411","PASSENGER",3,60,360,12.0,0,0,"RUNNING"));
    }

    public synchronized Map<String,Object> section() {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("name","Demo Section: Navapura – Devgarh");
        m.put("lengthKm",sectionLengthKm); m.put("loopPointsKm",loopPointsKm);
        m.put("simulationMinute",simulationMinute); m.put("lineType","SINGLE_LINE_WITH_LOOPS");
        m.put("safetyMode","SIMULATION_ONLY");
        return m;
    }

    public synchronized List<Train> trains() { return new ArrayList<>(trains.values()); }
    public synchronized List<Recommendation> recommendations() { return new ArrayList<>(recommendations.values()); }

    public synchronized Map<String,Object> tick() {
        simulationMinute++;
        for (Train t : trains.values()) {
            if (!"RUNNING".equals(t.status)) continue;
            double movement = t.speedKmph / 60.0;
            t.positionKm = Math.min(sectionLengthKm, Math.max(0, t.positionKm + (t.destinationKm >= t.positionKm ? movement : -movement)));
            if ((t.destinationKm >= t.positionKm && t.positionKm >= t.destinationKm) ||
                (t.destinationKm < t.positionKm && t.positionKm <= t.destinationKm)) t.status="ARRIVED";
        }
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("simulationMinute",simulationMinute);
        result.put("trains",trains());
        result.put("conflicts",detectConflicts());
        return result;
    }

    public synchronized List<Map<String,Object>> detectConflicts() {
        List<Map<String,Object>> conflicts = new ArrayList<>();
        List<Train> active = trains.values().stream().filter(t -> "RUNNING".equals(t.status)).toList();
        for (int i=0;i<active.size();i++) for (int j=i+1;j<active.size();j++) {
            Train a=active.get(i), b=active.get(j);
            boolean opposing = (a.destinationKm-a.positionKm)*(b.destinationKm-b.positionKm)<0;
            double separation = Math.abs(a.positionKm-b.positionKm);
            if (opposing && separation < 5.0) {
                Map<String,Object> c=new LinkedHashMap<>();
                c.put("type","SINGLE_LINE_MEETING"); c.put("trainA",a.id); c.put("trainB",b.id);
                c.put("separationKm",Math.round(separation*100.0)/100.0);
                c.put("severity",separation<2.0?"HIGH":"MEDIUM");
                c.put("message","Opposing trains are approaching within the simulated conflict zone.");
                conflicts.add(c);
            }
        }
        return conflicts;
    }

    public synchronized Recommendation recommend() {
        List<Train> active = trains.values().stream().filter(t -> "RUNNING".equals(t.status)).toList();
        if (active.size()<2) return null;
        // Baseline heuristic: choose the train with highest priority; break ties by lower delay,
        // then by nearest distance to its destination. This is a demo policy, not a safety authority.
        List<Train> ranked = new ArrayList<>(active);
        ranked.sort(Comparator.comparingInt((Train t)->t.priority)
                .thenComparingInt(t->t.delayMinutes)
                .thenComparingDouble(t->Math.abs(t.destinationKm-t.positionKm)));
        Train first=ranked.get(0), second=ranked.get(1);
        String reason="Priority rank "+first.priority+" is highest among active trains; controller review is required. " +
                "The recommendation is only a simulated precedence suggestion.";
        Recommendation r=new Recommendation("REC-"+ids.getAndIncrement(),first.id,second.id,reason,
                Math.max(1,second.delayMinutes-first.delayMinutes+4));
        recommendations.put(r.id,r);
        return r;
    }

    public synchronized Recommendation decide(String id, String decision) {
        Recommendation r=recommendations.get(id);
        if (r==null) throw new NoSuchElementException("Recommendation not found: "+id);
        if (!List.of("ACCEPT","REJECT").contains(decision)) throw new IllegalArgumentException("Decision must be ACCEPT or REJECT");
        r.status=decision.equals("ACCEPT")?"ACCEPTED_BY_CONTROLLER":"REJECTED_BY_CONTROLLER";
        return r;
    }
}
