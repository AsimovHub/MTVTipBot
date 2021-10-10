package ac.asimov.mtvtipbot.service;

import ac.asimov.mtvtipbot.dao.TransactionDao;
import ac.asimov.mtvtipbot.dao.UserDao;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.helper.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class StatisticsService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    @Autowired
    private TransactionDao transactionDao;

    public ResponseWrapperDto<Integer> getTotalUserCount() {
        try {
            return new ResponseWrapperDto<>(userDao.findAll().size());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>(DefaultMessage.SERVER_ERROR);
        }
    }

    public ResponseWrapperDto<Integer> getTotalTransactionCount() {
        try {
            return new ResponseWrapperDto<>(transactionDao.findAll().size());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>(DefaultMessage.SERVER_ERROR);
        }
    }

    public ResponseWrapperDto<String> getTransaction(String transactionHash) {
        try {
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("hash", transactionHash);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://e.mtv.ac/transaction/get";
            ResponseEntity<String> transactionResponse = restTemplate.postForEntity(url, request , String.class );

            if (transactionResponse.getStatusCode() != HttpStatus.OK) {
                logger.error("Error fetching transaction: " + transactionResponse.getStatusCode());
                throw new Exception("Error fetching transaction: " + transactionResponse.getStatusCode());
            }
            String jsonString = transactionResponse.getBody();
            ResponseWrapperDto<String> response = new ResponseWrapperDto<>();
            response.setResponse(jsonString);
            return response;
        } catch (Exception e) {
            return new ResponseWrapperDto<>(DefaultMessage.SERVER_ERROR);
        }

    }
}
