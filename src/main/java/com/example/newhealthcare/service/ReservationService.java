package com.example.newhealthcare.service;

import com.example.newhealthcare.Header;
import com.example.newhealthcare.dto.DoctorResponseDTO;
import com.example.newhealthcare.dto.PatientResponseDTO;
import com.example.newhealthcare.dto.ReservationDoctorDTO;
import com.example.newhealthcare.dto.ReservationInfoDTO;
import com.example.newhealthcare.itf.CrudInterface;
import com.example.newhealthcare.model.entity.DandP;
import com.example.newhealthcare.model.entity.Patient;
import com.example.newhealthcare.model.entity.Reservation;
import com.example.newhealthcare.model.network.request.reservation.ReservationApiRequest;
import com.example.newhealthcare.model.network.request.ReservationDateApiRequest;
import com.example.newhealthcare.model.network.response.reservation.ReservationApiResponse;
import com.example.newhealthcare.model.network.response.reservation.ReservationDateApiResponse;
import com.example.newhealthcare.model.network.response.reservation.ReservationHomePApiResponse;
import com.example.newhealthcare.repository.DandPRepository;
import com.example.newhealthcare.repository.DoctorRepository;
import com.example.newhealthcare.repository.PatientRepository;
import com.example.newhealthcare.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService implements CrudInterface<ReservationApiRequest, ReservationApiResponse> {
    @Autowired
    private final ReservationRepository reservationRepository;
    @Autowired
    private final DoctorRepository doctorRepository;
    @Autowired
    private final PatientRepository patientRepository;
    @Autowired
    private final DandPRepository dandPRepository;

    @Override
    public Header<ReservationApiResponse> create(Header<ReservationApiRequest> request) {
        ReservationApiRequest requestData=request.getData();
        Optional<Patient> patient=patientRepository.findById(requestData.getPatientId());
        List<Reservation> reservation=
                reservationRepository.findBySelDoctorId(requestData.getSelDoctorId());

        if(!reservation.isEmpty()|| reservation!=null) {
            for(int i=0; i< reservation.size();i++){
                if(reservation.get(i).getResDate().equals(requestData.getResDate()) &&
                    reservation.get(i).getResTime()== requestData.getResTime()){
                    return Header.ERROR("이미 예약된 시간입니다.");

                }
            }
        }

        Reservation reservation1= Reservation.builder()
                .patientId(patient.get())
                .selDoctorId(requestData.getSelDoctorId())
                .contents(requestData.getContents())
                .resTime(requestData.getResTime())
                .resDate(requestData.getResDate())
                .resDate(requestData.getResDate())
                .build();
        reservationRepository.save(reservation1);

        return Header.OK();

    }

    @Override
    public Header<ReservationApiResponse> read(String id) {
        return null;
    }

    //예약페이지 들어갈때 보내줄 데이터 (connect의사리스트 포함)
    public Header<ReservationHomePApiResponse> show(String id){
        Optional<Patient> patient=patientRepository.findById(id);
        List<DoctorResponseDTO> doctorResponseDTOS=new ArrayList<DoctorResponseDTO>();
        List<ReservationInfoDTO> reservationInfoDTOS=new ArrayList<ReservationInfoDTO>();

        if(patient.isPresent()){
            List<DandP> dandPS= patient.get().getDandpList();
            List<Reservation> reservation= reservationRepository.findByPatientId(patient.get());

            if(dandPS.isEmpty()) {
                System.out.println("danps.isempty()");
            }else{
                //connect의사 리스트들 보내줄때
                for(int i=0;i< dandPS.size();i++){

                    DoctorResponseDTO doctorResponseDTO= DoctorResponseDTO.builder()
                            .id(dandPS.get(i).getDoctorId().getDoctorId())
                            .name(dandPS.get(i).getDoctorId().getName())
                            .gender(dandPS.get(i).getDoctorId().getGender())
                            .major(dandPS.get(i).getDoctorId().getMajor())
                            .build();

                    doctorResponseDTOS.add(doctorResponseDTO);
                }

            }
            if(reservation.isEmpty()) {
                System.out.println("reservation.isempty()");
            }else{
                //예약정보 보내줄때
                for(int i=0;i< reservation.size();i++){
                    Reservation reservation1 = reservation.get(i);
                    ReservationInfoDTO reservationInfoDTO = ReservationInfoDTO.builder()
                            .doctorName(reservation1.getSelDoctorId())
                            .resDate(reservation1.getResDate())
                            .resTime(reservation1.getResTime())
                            .build();
                    reservationInfoDTOS.add(reservationInfoDTO);
                }
            }
        }
        ReservationHomePApiResponse response=
                ReservationHomePApiResponse.builder()
                        .doctor(doctorResponseDTOS)
                        .reservationList(reservationInfoDTOS)
                        .build();

        return Header.OK(response);
    }

    //예약시 가능한 의사별 시간대를 return
    public Header<ReservationDateApiResponse> showDate(String id, Header<ReservationDateApiRequest> request){
        ReservationDateApiRequest request1=request.getData();
        Optional<Patient> patient=patientRepository.findById(id);

        if(patient.isPresent()) {
            List<Integer> time= new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));

            //환자가 예약한 리스트들 return
            List<Reservation> reservationList= patient.get().getReservationList();

            for(int i=0;i<reservationList.size();i++){

                //예약에 있는 의사id 와 요청들어온 의사id가 같고, 날짜가 같을때
                if(reservationList.get(i).getSelDoctorId().equals(request1.getSelDoctorId()) &&
                        reservationList.get(i).getResDate().equals(request1.getResDate())){

                    //예약가능한 타임 = 8번 반복하며 이미 예약된 시간 제거
                    for(int j=0;j<time.size();j++) {
                        if (reservationList.get(i).getResTime() == time.get(j)){
                            time.remove(j);
                        }
                    }
                }
            }

            ReservationDateApiResponse response = ReservationDateApiResponse.builder()
                    .doctorId(request1.getSelDoctorId())
                    .resTime(time)
                    .build();

            return Header.OK(response);
        }else{
            return Header.ERROR("회원정보가 없습니다.");
        }
    }

    //의사가 예약환자 조회
    public Header<?> showPatientResList(String id){
        List<Reservation> reservation=reservationRepository.findBySelDoctorId(id);
        if(!reservation.isEmpty()){
            List<ReservationDoctorDTO> reservationDTOS=new ArrayList<ReservationDoctorDTO>();
            for( int i=0;i< reservation.size();i++) {
                PatientResponseDTO patientResponseDTO=PatientResponseDTO.builder()
                        .patientId(reservation.get(i).getPatientId().getPatientId())
                        .born(reservation.get(i).getPatientId().getBorn())
                        .gender(reservation.get(i).getPatientId().getGender())
                        .name(reservation.get(i).getPatientId().getName())
                        .phone(reservation.get(i).getPatientId().getPhone())
                        .build();

                ReservationDoctorDTO reservationDTO= ReservationDoctorDTO.builder()
                        .resNum(reservation.get(i).getResNum())
                        .patientId(patientResponseDTO)
                        .contents(reservation.get(i).getContents())
                        .selDoctorId(reservation.get(i).getSelDoctorId())
                        .resDate(reservation.get(i).getResDate())
                        .resTime(reservation.get(i).getResTime())
                        .build();
                reservationDTOS.add(reservationDTO);
            }
            return Header.OK(reservationDTOS);
        }
        return Header.ERROR("예약정보가 없습니다.");
    }

    @Override
    public Header<ReservationApiResponse> update(Header<ReservationApiRequest> request) {
        return null;
    }

    @Override
    public Header delete(String id) {
        return null;
    }
}
