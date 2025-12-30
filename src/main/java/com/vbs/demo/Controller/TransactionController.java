package com.vbs.demo.Controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")

public class TransactionController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj)
    {
        User user= userRepo.findById((obj.getId()))
                .orElseThrow(()-> new RuntimeException("not found"));
        double newbalance=user.getBalance()+obj.getAmount();
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction t=new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newbalance);
        t.setDescription("Rs "+obj.getAmount()+" deposit sucessfull");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "deposit sucessful ";
    }


    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj)
    {
        User user= userRepo.findById((obj.getId()))
                .orElseThrow(()-> new RuntimeException("not found"));
        double newbalance=user.getBalance()-obj.getAmount();
        if(newbalance<0)
        {
            return "balance not sufficient";
        }
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction t=new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newbalance);
        t.setDescription("Rs "+obj.getAmount()+" withdrawal sucessfull");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "withdrawal sucessful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj)
    {
        User sender=userRepo.findById(obj.getId())
                .orElseThrow(()->new RuntimeException("Not Found"));
        User rec=userRepo.findByUsername(obj.getUsername());

        if(rec==null)
        {
            return "Username not found";
        }
        if(obj.getAmount()<1)
        {
            return "Invalid Amount";
        }
        if(sender.getId()== rec.getId())
        {
            return "Self transfer not allowed";
        }

        double sBalance= sender.getBalance()-obj.getAmount();
        double rBalance=rec.getBalance()+obj.getAmount();
        if(sBalance<0)
        {
            return "Insufficient balance";
        }
        sender.setBalance(sBalance);
        rec.setBalance(rBalance);

        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1 = new Transaction();
        Transaction t2=new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sBalance);
        t1.setDescription("Rs "+obj.getAmount()+" Sent to "+obj.getUsername());
        t1.setUserId(obj.getId());

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rBalance);
        t2.setDescription("Rs "+obj.getAmount()+" received from "+sender.getUsername());
        t2.setUserId(rec.getId());

        transactionRepo.save(t1);
        transactionRepo.save(t2);
        return "Transfer successfully";

    }


    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id)
    {
        return transactionRepo.findAllByUserId(id);
    }


}
