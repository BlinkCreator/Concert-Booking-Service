package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Seat;

import java.util.ArrayList;
import java.util.List;

//converts Seat Domain objects used in Seat methods into DTO objects
public class SeatMapper {
    public static SeatDTO MakeDTO(Seat seat){
        return new SeatDTO(seat.getLabel(), seat.getPrice());
    }

    public static List<SeatDTO> MakeDTOList (List<Seat> seatList) {
        List<SeatDTO> _SeatDTO = new ArrayList<>();

        for (Seat _seat: seatList) {
            _SeatDTO.add(SeatMapper.MakeDTO(_seat));
        }

        return _SeatDTO;
    }
}
