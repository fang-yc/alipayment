package com.ff.alipayment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.ff.zfbpayment.bean.AlipayVo;
import com.ff.zfbpayment.config.AlipayConfig;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;




/**
 * @Description 支付宝后台接口
 * @Author 方云聪
 * @Date 2019/9/11 13:08
 */

@RequestMapping("alipay")
@RestController
public class AlipayController {




    @Autowired
    private AlipayClient alipayClient;
    
    
    @GetMapping("pay")
    private String alipayPay() throws AlipayApiException {



        //此处应从前台传回，为了测试直接在后台写死了
        AlipayVo alipayVo = new AlipayVo();
        //UUID.randomUUID().toString().replace("-","")  自动生成主键
        alipayVo.setOut_trade_no(UUID.randomUUID().toString().replace("-",""));

        alipayVo.setTotal_amount("0.01");

        alipayVo.setSubject("jik");

        alipayVo.setProduct_code("FAST_INSTANT_TRADE_PAY");//固定的

        //Gson有两个方法   toJson 把bean变成json串  fromJson把json串变成bean
        String json = new Gson().toJson(alipayVo);

        //设置请求参数
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_url);
        alipayTradePagePayRequest.setBizContent(json);
//        alipayRequest.setBizContent("{" +
//                    "    \"out_trade_no\":\"20150320010101001\"," +
//                    "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                    "    \"total_amount\":88.88," +
//                    "    \"subject\":\"Iphone6 16G\"," +
//                    "    \"body\":\"Iphone6 16G\"," +
//                    "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                    "    \"extend_params\":{" +
//                    "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                    "    }"+
//                    "  }");//填充业务参数

        String result  = alipayClient.pageExecute(alipayTradePagePayRequest).getBody();//调用sdk生成表单

        System.out.println(result);

        return result;
    }

    /**
     * @Title: alipayNotify
     * @Description:    支付宝异步通知回调接口
     * @author nelson
     * @param request
     * @param out_trade_no 商户订单号
     * @param trade_no 支付宝交易凭证号
     * @param trade_status 交易状态
     * @throws AlipayApiException
     * @return String
     * @throws
     */
    @PostMapping("notify")
    private String alipayNotify(HttpServletRequest request, String out_trade_no,String trade_no,String trade_status)
    throws AlipayApiException{
        Map<String, String> map = new HashMap<>();

        //request.getParameterMap()从前端传回的返回值只能读
        Map<String,String[]> requestParameterMap = request.getParameterMap();
        for (Iterator<String> iter = requestParameterMap.keySet().iterator();iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParameterMap.get(name);
            String valueStr = "";
            for(int i = 0; i < values.length; i++){
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                System.out.println(valueStr);
            }
            map.put(name,valueStr);
        }
        boolean signVerfied = false;
        try {
            signVerfied = AlipaySignature.rsaCheckV1(map, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        }catch (AlipayApiException e){
            e.printStackTrace();
            return ("fail");//验签异常返回失败
        }
        if(signVerfied){
            //添加业务逻辑，更新订单状态等


            return ("success");
        }else {
            System.out.println("验签失败，不去做任何更新");
            return ("fail");
        }

    }

    /**
     * @Title: alipayReturn
     * @Description: 支付宝回调接口
     * @author nelson
     * @param request
     * @param out_trade_no 商户订单号
     * @param trade_no 支付宝交易凭证号
     //* @param trade_status 交易状态
     * @throws AlipayApiException
     * @return String
     * @throws
     */
    @GetMapping("return")
    private String alipayReturn(Map<String,String> params,HttpServletRequest request,String out_trade_no,String trade_no,String total_amount)
            throws AlipayApiException {
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                System.out.println(valueStr);
            }
            map.put(name, valueStr);
        }
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(map,AlipayConfig.alipay_public_key,AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return ("fail");// 验签发生异常,则直接返回失败
        }
        if (signVerified) {
            //处理你的业务逻辑，更细订单状态等
            return ("success");
        } else {
            System.out.println("验证失败,不去更新状态");
            return ("fail");
        }

    }



    
}
