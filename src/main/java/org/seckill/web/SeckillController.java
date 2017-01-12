package org.seckill.web;

import java.util.Date;
import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller//@Service @Component
@RequestMapping("/seckill") //url:/妯″潡/璧勬簮/{id}/缁嗗垎 /seckill/list
public class SeckillController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private SeckillService seckillService;
	/**
	 * 鍒楄〃椤�
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model model){
		//鑾峰彇鍒楄〃椤�
		List<Seckill> list = seckillService.getSeckillList();
		model.addAttribute("list", list);
		//list.jsp + model(鏁版嵁) = ModelAndView
		return "list";
	}
	
	/**
	 * 璇︽儏椤�
	 */
	@RequestMapping(value = "/{seckillId}/detail", method=RequestMethod.GET)
	public String detail(@PathVariable("seckillId") Long seckillId, Model model){
		if(seckillId == null){
			return "redirect:/seckill/list";
		}
		Seckill seckill = seckillService.getById(seckillId);
		if(seckillId == null){
			return "forward:/seckill/list";
		}
		model.addAttribute("seckill", seckill);
		return "detail";
	}

	//杈撳嚭绉掓潃鐨勪俊鎭�
	//ajax json
	@RequestMapping(value = "/{seckillId}/exposer",
			method = RequestMethod.POST,
			produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId){
		SeckillResult<Exposer> result;
		try {
			Exposer exposer = seckillService.exportSeckillUrl(seckillId);
			result = new SeckillResult<Exposer>(true, exposer);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result = new SeckillResult<Exposer>(false,e.getMessage());
		}
		return result;
	}
	
	@RequestMapping(value="/{seckillId}/{md5}/execution",
			method=RequestMethod.POST,
			produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
												   @PathVariable("md5") String md5,
												   //鎵嬫満鍙风爜淇濆瓨鍦╟ookie閲岄潰锛宺equired(鏀瑰弬鏁版槸鍚︿负蹇呴』锛岃繖閲岃涓篺alse琛ㄧず娌℃湁杩欎釜鍙傛暟spring涔熶笉浼氭姤閿�)
												   @CookieValue(value="killPhone",required=false) Long phone){
		//springmvc valid
		if(phone == null){
			return new SeckillResult<SeckillExecution>(false, "鏈敞鍐�");
		}
		SeckillResult<SeckillExecution> result;
		try {
			SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
			return new SeckillResult<SeckillExecution>(true, execution);
		} catch(RepeatKillException e1){
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
			return new SeckillResult<SeckillExecution>(true, execution);
		}catch(SeckillCloseException e2){
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
			return new SeckillResult<SeckillExecution>(true, execution);
		}catch (SeckillException e) {
			logger.error(e.getMessage(),e);
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
			return new SeckillResult<SeckillExecution>(true, execution);
		}
	}
	
	@RequestMapping(value = "/time/now" ,method = RequestMethod.GET)
	@ResponseBody
	public SeckillResult<Long> time(){
		Date now = new Date();
		return new SeckillResult(true, now.getTime());
	}
}
