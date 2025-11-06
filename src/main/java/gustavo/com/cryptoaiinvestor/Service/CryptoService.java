package gustavo.com.cryptoaiinvestor.Service;

import gustavo.com.cryptoaiinvestor.DTO.CryptoFrontEndDto;
import gustavo.com.cryptoaiinvestor.Models.Crypto;
import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoService {
    private final CryptoRepository cryptoRepository;

    public CryptoService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }


    public List<CryptoFrontEndDto> getAllCryptosForMainPage() {
        List<Crypto> allCryptos = cryptoRepository.findAll();

        List<CryptoFrontEndDto> frontEndDtoList = allCryptos.stream().map(c -> new CryptoFrontEndDto(
                c.getTicker(),
                c.getName(),
                c.getImg()
        )).toList();

        return frontEndDtoList;
    }
}
