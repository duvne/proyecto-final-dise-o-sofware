package com.volt.operaciones.presentation;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.operaciones.business.SmartActorService;

@RestController
@RequestMapping("/api/ops/smart")
public class SmartActorController {
	private final SmartActorService smartActorService;

	public SmartActorController(SmartActorService smartActorService) {
		this.smartActorService = smartActorService;
	}

	@GetMapping("/report")
	public Map<String, Object> report(
			@RequestParam(required = false) String idReserva,
			@RequestParam(required = false) String intents,
			@RequestParam(defaultValue = "false") boolean signedAction) {
		return smartActorService.process(idReserva == null ? "global" : idReserva,
				intents == null ? "true" : intents, signedAction);
	}

	@PostMapping("/remitente")
	public Map<String, Object> sendAInteress2Specgrave(
			@RequestParam String idReserva,
			@RequestParam(required = false) String intents,
			@RequestParam(defaultValue = "true") boolean signedAction) {
		return smartActorService.process(idReserva, intents == null ? "true" : intents, signedAction);
	}
}
