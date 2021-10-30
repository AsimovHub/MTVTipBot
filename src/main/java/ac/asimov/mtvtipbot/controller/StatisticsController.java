package ac.asimov.mtvtipbot.controller;

import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.TipbotStatisticsResponseDto;
import ac.asimov.mtvtipbot.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@CrossOrigin(allowedHeaders = "*", originPatterns = "*", maxAge = 3600, methods = {GET, POST, OPTIONS, PUT, DELETE})
public class StatisticsController {

    @Autowired
    private StatisticsService service;

    @RequestMapping(method = RequestMethod.GET, value = "/tipbot/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapperDto<TipbotStatisticsResponseDto> getTipbotStatistics() {
        return service.getTipbotStatistics();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tipbot/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapperDto<Integer> getTotalUserCount() {
        return service.getTotalUserCount();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tipbot/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapperDto<Integer> getTotalTransactionCount() {
        return service.getTotalTransactionCount();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tipbot/transactions/{transactionHash}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapperDto<String> getTransaction(@PathVariable("transactionHash") String transactionHash) {
        return service.getTransaction(transactionHash);
    }

}
