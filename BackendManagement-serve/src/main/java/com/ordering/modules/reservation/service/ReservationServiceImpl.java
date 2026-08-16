package com.ordering.modules.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.reservation.dto.ReservationCreateDTO;
import com.ordering.modules.reservation.entity.DiningTable;
import com.ordering.modules.reservation.entity.Reservation;
import com.ordering.modules.reservation.mapper.DiningTableMapper;
import com.ordering.modules.reservation.mapper.ReservationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final DiningTableMapper diningTableMapper;

    public ReservationServiceImpl(ReservationMapper reservationMapper, DiningTableMapper diningTableMapper) {
        this.reservationMapper = reservationMapper;
        this.diningTableMapper = diningTableMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long shopId, Long memberId, ReservationCreateDTO dto) {
        Reservation r = new Reservation();
        r.setShopId(shopId);
        r.setMemberId(memberId);
        r.setTableId(dto.getTableId());
        try {
            r.setDate(LocalDate.parse(dto.getDate()));
        } catch (Exception e) {
            throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "日期格式应为 yyyy-MM-dd");
        }
        r.setTimeSlot(dto.getTimeSlot());
        r.setPartySize(dto.getPartySize());
        r.setDeposit(dto.getDeposit() == null ? 0 : dto.getDeposit());
        r.setStatus(0); // 待确认
        reservationMapper.insert(r);
        return r.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long shopId, Long memberId, Long id) {
        Reservation r = requireMine(shopId, memberId, id);
        if (r.getStatus() != 0 && r.getStatus() != 1) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "当前状态不可取消");
        }
        r.setStatus(3); // 取消
        reservationMapper.updateById(r);
    }

    @Override
    public List<Reservation> listMine(Long shopId, Long memberId) {
        return reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getShopId, shopId).eq(Reservation::getMemberId, memberId)
                .orderByDesc(Reservation::getId));
    }

    @Override
    public List<Reservation> adminList(Long shopId) {
        return reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getShopId, shopId).orderByDesc(Reservation::getId));
    }

    @Override
    public List<DiningTable> listTables(Long shopId) {
        return diningTableMapper.selectList(new LambdaQueryWrapper<DiningTable>()
                .eq(DiningTable::getShopId, shopId)
                .orderByAsc(DiningTable::getArea)
                .orderByAsc(DiningTable::getSeats));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long shopId, Long id) {
        Reservation r = requireShop(shopId, id);
        if (r.getStatus() != 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "仅待确认的预订可确认");
        }
        r.setStatus(1); // 已确认
        reservationMapper.updateById(r);
        // 指定桌则标记桌台为「预定(2)」
        if (r.getTableId() != null) {
            DiningTable t = diningTableMapper.selectOne(new LambdaQueryWrapper<DiningTable>()
                    .eq(DiningTable::getId, r.getTableId()).eq(DiningTable::getShopId, shopId));
            if (t != null) {
                DiningTable upd = new DiningTable();
                upd.setId(t.getId());
                upd.setStatus(2);
                upd.setVersion(t.getVersion());
                diningTableMapper.updateById(upd);
            }
        }
    }

    private Reservation requireMine(Long shopId, Long memberId, Long id) {
        Reservation r = reservationMapper.selectOne(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id).eq(Reservation::getShopId, shopId).eq(Reservation::getMemberId, memberId));
        if (r == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "预订不存在");
        }
        return r;
    }

    private Reservation requireShop(Long shopId, Long id) {
        Reservation r = reservationMapper.selectOne(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getId, id).eq(Reservation::getShopId, shopId));
        if (r == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "预订不存在");
        }
        return r;
    }
}
