package com.example.clientservice.controller;

import com.example.clientservice.model.ScoringResponse;
import com.example.clientservice.service.NewsClientService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final NewsClientService clientService;

    // Явный конструктор — Spring вызовет его автоматически
    public ClientController(NewsClientService clientService) {
        this.clientService = clientService;
    }

   
@GetMapping("/sync")
public Mono<ScoringResponse> scoreSync(@RequestParam String topic) {
    return Mono.fromCallable(() -> clientService.getScoredNewsSync(topic))
               .subscribeOn(Schedulers.boundedElastic());
}

    @GetMapping("/async")
    public Mono<ScoringResponse> scoreAsync(@RequestParam String topic) {
        return clientService.getScoredNewsAsync(topic);
    }

    @GetMapping("/test")
    public String test() {
        return "Client Service is running!";
    }
}