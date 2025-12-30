package com.vbs.demo.Controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.Logindto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
//building controller

public class UserController {
   //classname object name and autowired used to get object name of interface

    @Autowired
    UserRepo userRepo;

    @Autowired
    HistoryRepo historyRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        return "signup sucessfull";

    }

    /// //////////////////////////////////////////////////////////////////////
    @PostMapping("/login")
    public String login(@RequestBody Logindto u) {
     User user= userRepo.findByUsername(u.getUsername());
     // will filter the user by matching user form login
     if (user==null)
     {
         return "user not found";

     }
     if(!u.getPassword().equals(user.getPassword()))
         //comapring login user with database wala user
     {
         return "password incorrect";
     }
     if(!u.getRole().equals(user.getRole()))
     {
         return "role incorrect";
     }
     return String.valueOf(user.getId());
     //since we wnat the if as welcome in homepage so we convert if into string and return string

    }
    /// /////////////////////////////////////////////////////////

    //FOR DISPLAYING USERNAME AND BALANCE AFTER LOGIN AND JUST AFTER LANDING ON DASHBOARD
    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)//INT ID IS WHERE WE STORE THE ID WE GOT
    {
        User user= userRepo.findById(id).orElseThrow(()->new RuntimeException("user not found"));
        DisplayDto displaydto=new DisplayDto();

        displaydto.setUsername(user.getUsername());
        displaydto.setBalance(user.getBalance());


        return displaydto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user= userRepo.findById(obj.getId())
                .orElseThrow(()-> new RuntimeException("user not found"));

        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(obj.getValue().equalsIgnoreCase(user.getName()))
            {
                return "name cannot be same";
            }
            user.setName(obj.getValue());
        }
        else if (obj.getKey().equalsIgnoreCase("password"))
        {
            if(obj.getValue().equalsIgnoreCase(user.getPassword()))
            {
                return "Password cannot be same";
            }
            user.setPassword(obj.getValue());
        }
        else if (obj.getKey().equalsIgnoreCase("email"))
        {
            if(obj.getValue().equalsIgnoreCase(user.getEmail()))
            {
                return "Email cannot be same";
            }
            User user2=userRepo.findByEmail(obj.getValue());
            if(user2 != null)
            {
                return "Email already exists";
            }

            user.setEmail(obj.getValue());
        }
        else {
            return "invalid choice";
        }
        userRepo.save(user);
        return "Updated sucessfully";

    }
    //admin creates and add user
    //@PostMapping("/add")//
   /*public String add(@RequestBody User user)
    {
        userRepo.save(user);
        return "Added user sucessfully";
    }*/

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId)
    {
        History h1=new History();
        h1.setDescription("Admin "+adminId+" created user "+user.getUsername());
        userRepo.save(user);

        if(user.getBalance()>0)
        {
            User user2=userRepo.findByUsername(user.getUsername());
            Transaction t=new Transaction();
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs "+user.getBalance()+" Deposit Sucessful");
            t.setUserId(user2.getId());
            transactionRepo.save(t);


        }




        historyRepo.save(h1);
        return "Added user sucessfully";
    }
    //request param is used when we require an parameter for filtering but it works even if we dont get the parameter



    @DeleteMapping("delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId,@PathVariable int adminId)
    {
        User user=userRepo.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        if(user.getBalance()>0)
        {
            return "balance must be zero";
        }
        History h1=new History();
        h1.setDescription("Admin "+adminId+" deleted user "+user.getUsername());
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Sucessfully";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy, @RequestParam String order)
    {
        Sort sort;


        //need if else for desc & asce because they dont get call itself but sort get called it self
        // and manages all sorting itself so we dont k=need if else for all sorting


        if(order.equalsIgnoreCase("desc"))
        {
            sort=Sort.by(sortBy).descending();
        }
        else {
            sort=Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer",sort);//filtered by role since we dont want admin while showing user in admin panel
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

}
