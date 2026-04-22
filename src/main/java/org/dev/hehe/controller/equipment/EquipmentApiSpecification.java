package org.dev.hehe.controller.equipment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.equipment.EquipmentResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Equipment API Swagger 명세 인터페이스
 * - Swagger 어노테이션만 정의
 * - 실제 구현은 EquipmentController
 * - Hospital 그룹(03) 내 Equipment 섹션으로 노출
 */
@Tag(name = "Equipment", description = "메인 화면 기기 API")
public interface EquipmentApiSpecification {

    @Operation(summary = "메인 노출 기기 목록 조회",
            description = "홈 화면에 노출할 기기 목록을 반환합니다. is_main_display=true 인 기기만 display_order 순으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        { "equipId": 1, "modelName": "젠틀맥스프로플러스", "displayOrder": 1 },
                                        { "equipId": 2, "modelName": "아포지플러스",       "displayOrder": 2 },
                                        { "equipId": 3, "modelName": "클라리티2",          "displayOrder": 3 }
                                      ]
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "C001",
                                      "message": "서버 내부 오류가 발생했습니다."
                                    }
                                    """))
            )
    })
    ApiResult<List<EquipmentResponse>> getMainEquipments();


    @Operation(summary = "기기 단건 조회",
            description = "equipId로 특정 기기를 조회합니다. 존재하지 않으면 404를 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "equipId": 1,
                                        "modelName": "젠틀맥스프로플러스",
                                        "displayOrder": 1
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "기기 없음 (H002)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "errorCode": "H002",
                                      "message": "기기를 찾을 수 없습니다."
                                    }
                                    """))
            )
    })
    ApiResult<EquipmentResponse> getEquipmentById(
            @Parameter(description = "조회할 기기 ID") @PathVariable Long equipId);
}