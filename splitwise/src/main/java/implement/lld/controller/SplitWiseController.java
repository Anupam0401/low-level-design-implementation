package implement.lld.controller;

import implement.lld.service.SplitWiseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/split_wise")
public class SplitWiseController {

    private final SplitWiseService splitWiseService;

    // Add expense
    @GetMapping("/add_expense")
    public boolean addExpense() {
        return ResponseEntity.ok(splitWiseService.addExpense());
    }
}
