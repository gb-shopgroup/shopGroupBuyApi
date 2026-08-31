package cn.com.shopgroup.order.controller.leader;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
public class OrderRefundNotifyController {

    // 退款结果回调通知(占位接口)
    @PostMapping("/leader/refund/notify")
    public String notify(HttpServletRequest req){

        return "success";
    }

}
