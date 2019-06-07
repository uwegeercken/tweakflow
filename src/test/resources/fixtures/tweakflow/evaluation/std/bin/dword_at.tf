import bin from 'std.tf';
import expect, expect_error, to from "std/assert.tf";

alias bin.dword_at as dword_at;

library dword_at_spec {

  of_nil_nil:
    expect(dword_at(nil, nil), to.be_nil());

  of_empty_0:
    expect(dword_at(0b, 0), to.be_nil());

  of_empty_10:
    expect(dword_at(0b, 10), to.be_nil());

  of_empty_neg_1:
    expect(dword_at(0b, -1), to.be_nil());

  of_0b00_0:
    expect(dword_at(0b00, 0), to.be_nil());

  of_0bFF_0:
    expect(dword_at(0bFF, 0), to.be_nil());

  of_0b00000000_0:
    expect(dword_at(0b00000000, 0), to.be(0));

  of_0b00000000_0_big_endian:
    expect(dword_at(0b00000000, 0, true), to.be(0));

  of_0bFF000000_0:
    expect(dword_at(0bFF000000, 0), to.be(255));

  of_0bFF000000_0_big_endian:
    expect(dword_at(0bFF000000, 0, true), to.be(4278190080));

  of_0bFFFFFFFF_0:
    expect(dword_at(0bFFFFFFFF, 0), to.be(4294967295));

  of_0bFFFFFFFF_0_big_endian:
    expect(dword_at(0bFFFFFFFF, 0, true), to.be(4294967295));

  of_0b000102030405060708090a0b0c0d0e0f_0:
    expect(dword_at(0b000102030405060708090a0b0c0d0e0f, 0), to.be(0x03020100));

  of_0b000102030405060708090a0b0c0d0e0f_0_big_endian:
    expect(dword_at(0b000102030405060708090a0b0c0d0e0f, 0, true), to.be(0x00010203));

  of_0b000102030405060708090a0b0c0d0e0f_8:
    expect(dword_at(0b000102030405060708090a0b0c0d0e0f, 8), to.be(0x0b0a0908));

  of_0b000102030405060708090a0b0c0d0e0f_8_big_endian:
    expect(dword_at(0b000102030405060708090a0b0c0d0e0f, 8, true), to.be(0x08090a0b));

}