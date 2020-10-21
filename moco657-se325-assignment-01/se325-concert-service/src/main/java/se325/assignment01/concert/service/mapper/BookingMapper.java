package se325.assignment01.concert.service.mapper;


import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.service.domain.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//converts Booking objects used in Booking methods into DTO objects

public class BookingMapper {

        public static BookingDTO MakeDTO(Booking booking){
            return new BookingDTO(booking.getConcertId(), booking.getDate(),
                    booking.getSeats().stream().map(_seat -> SeatMapper.MakeDTO(_seat)).collect(Collectors.toList()));
        }

        public static List<BookingDTO> MakeDTOList(List<Booking> bookingList) {
            ArrayList<BookingDTO> _bookDTOList = new ArrayList<>();

            for (Booking _booking: bookingList) {
                _bookDTOList.add(BookingMapper.MakeDTO(_booking));
            }

            return _bookDTOList;
        }

}
