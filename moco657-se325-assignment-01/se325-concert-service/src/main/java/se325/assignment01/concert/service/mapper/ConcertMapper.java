package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.ConcertSummaryDTO;
import se325.assignment01.concert.service.domain.Concert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//converts Domain objects used in Concert methods into DTO objects
public class ConcertMapper {
    public static ConcertDTO MakeDTO(Concert concert) {

        ConcertDTO _concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getImageName(), concert.getBlurb());
        _concertDTO.setDates(new ArrayList<LocalDateTime>(concert.getDates()));
        _concertDTO.setPerformers(PerformerMapper.MakeDTOSet(concert.getPerformers()));

        return _concertDTO;

    }

    public static List<ConcertDTO> MakeDTOList (List<Concert> concertList) {
        List<ConcertDTO> _DTOList = new ArrayList<>();
        for (Concert _concert : concertList) {
            _DTOList.add(ConcertMapper.MakeDTO(_concert));
        }
        return _DTOList;
    }

    public static ConcertSummaryDTO MakeDTOSummary(Concert concert) {
        return new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
    }

    public static List<ConcertSummaryDTO> MakeDTOSummaryList(List<Concert> concerts) {
        List<ConcertSummaryDTO>  _SummaryDTOList = new ArrayList<>();
        for (Concert _concert : concerts) {
            _SummaryDTOList.add(ConcertMapper.MakeDTOSummary(_concert));
        }
        return _SummaryDTOList;
    }
}