package app.newellgames.web.controllers;

import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final UserService userService;
    private final TransactionService transactionService;

    public TransactionController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @GetMapping("/my-transactions")
    public ModelAndView getMyTransactions(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        List<Transaction> myTransactions = userService.getTransactions(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transactions");
        modelAndView.addObject("user", user);
        modelAndView.addObject("myTransactions", myTransactions);

        return modelAndView;
    }

    @GetMapping ("/admin/all-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllTransactions(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @RequestParam(value = "query", required = false) String query) {
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Transaction> allTransactions;

        if(query != null && !query.isBlank()) {
            allTransactions = transactionService.getTransactionByOwnerIdOrUsername(query);
        } else {
            allTransactions = transactionService.getAllTransactions();
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("all-transactions");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allTransactions", allTransactions);
        modelAndView.addObject("query", query);

        return modelAndView;
    }
}
