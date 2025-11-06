package gustavo.com.cryptoaiinvestor.Controller;

import gustavo.com.cryptoaiinvestor.DTO.CryptoFrontEndDto;

import gustavo.com.cryptoaiinvestor.Service.CryptoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cryptos")
public class CryptoController {
    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/frontenddto")
    public ResponseEntity<List<CryptoFrontEndDto>> getAllCryptosForMainPage(){
        return ResponseEntity.status(HttpStatus.OK).body(cryptoService.getAllCryptosForMainPage());
    }
}
