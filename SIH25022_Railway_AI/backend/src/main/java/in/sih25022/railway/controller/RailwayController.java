package in.sih25022.railway.controller;

import in.sih25022.railway.model.Recommendation;
import in.sih25022.railway.service.RailwayService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173"})
public class RailwayController {
    private final RailwayService service;
    public RailwayController(RailwayService service) { this.service=service; }

    @GetMapping("/health")
    public Map<String,String> health() { return Map.of("status","UP","mode","SIMULATION_ONLY"); }

    @GetMapping("/section")
    public Map<String,Object> section() { return service.section(); }

    @GetMapping("/trains")
    public Object trains() { return service.trains(); }

    @GetMapping("/conflicts")
    public Object conflicts() { return service.detectConflicts(); }

    @GetMapping("/recommendations")
    public Object recommendations() { return service.recommendations(); }

    @PostMapping("/simulation/tick")
    public Object tick() { return service.tick(); }

    @PostMapping("/scheduling/recommend")
    public Object recommend() {
        Recommendation r=service.recommend();
        return r==null?Map.of("message","Not enough active trains"):r;
    }

    @PostMapping("/recommendations/{id}/decision")
    public Object decide(@PathVariable String id, @RequestBody Map<String,String> body) {
        return service.decide(id,body.getOrDefault("decision",""));
    }
}
