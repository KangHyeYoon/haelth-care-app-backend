package com.example.newhealthcare.controller;

import com.example.newhealthcare.Header;
import com.example.newhealthcare.dto.dandpdto.DandPResponseDTO;
import com.example.newhealthcare.itf.CrudInterface;
import com.example.newhealthcare.model.entity.Patient;
import com.example.newhealthcare.model.network.request.DandPApiRequest;
import com.example.newhealthcare.model.network.request.PatientApiRequest;
import com.example.newhealthcare.model.network.response.DandPApiResponse;
import com.example.newhealthcare.model.network.response.PatientApiResponse;
import com.example.newhealthcare.service.DandPService;
import com.example.newhealthcare.service.PatientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/patient")
public class PatientController implements CrudInterface<PatientApiRequest, PatientApiResponse> {
    @Autowired
    private PatientService patientService;

    @Autowired
    private DandPService dandPService;

    @PostMapping("/login")
    public Header<PatientApiResponse> login(@RequestBody Header<PatientApiRequest> request){
        log.info("patinet login: {}",request);
        return patientService.login(request);
    }

    @Override
    @PostMapping("/signup")
    public Header<PatientApiResponse> create(@RequestBody Header<PatientApiRequest> request) {
        log.info("{}",request);
        return patientService.create(request);
    }

    @Override
    @GetMapping("{id}")
    public Header<PatientApiResponse> read(@PathVariable String id) {
        log.info("read id : {}",id);
        return patientService.read(id);
    }


    /*====================== 환자와 의사 코드 관련 ==================*/

    //환자 코드 입력
    @PostMapping("{id}/code")
    public Header inputCode(@RequestBody Header<PatientApiRequest> request){
        Header<DandPApiResponse> dandPApiResponse= patientService.inputCode(request);
        if(dandPApiResponse.getResult().equals("Fail")){
            return dandPApiResponse;
        }else{
            return dandPService.create(dandPApiResponse);
        }
    }

    //환자와 연결된 의사 명단 출력
    @GetMapping("{id}/conDocList")
    public Header<PatientApiResponse> showDoctorList(@PathVariable String id){
        return patientService.showDoctorList(id);
    }

    /* ============================ 환자 info =======================*/

    //환자 정보 수정
    @Override
    @PutMapping("{id}/info/update")
    public Header<PatientApiResponse> update(Header<PatientApiRequest> request) {
        return patientService.update(request);
    }

    //환자 회원 삭제
    @Override
    @DeleteMapping("{id}/info/delete")
    public Header<PatientApiResponse> delete(@PathVariable String id) {
        log.info("delete id: "+id);
        return patientService.delete(id);
    }

}
